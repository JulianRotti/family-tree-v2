"use client"

import { motion, AnimatePresence } from "framer-motion"
import { useState, useMemo } from "react"
import { familyMembers, getMemberById, type FamilyMember, type PartnerRelation, type PartnerType } from "@/lib/mock-data"
import { cn } from "@/lib/utils"
import { User, Plus, Heart, Users, MapPin, Calendar, Briefcase, FileText, X, Check, Mail, Phone, Home } from "lucide-react"
import Image from "next/image"
import { GenderIndicator } from "@/components/gender-indicator"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { CreatePersonDialog, type PersonFormData } from "@/components/create-person-dialog"

interface MemberCardProps {
  member: FamilyMember
  isSelected: boolean
  onSelect: (id: string) => void
}

function MemberCard({ member, isSelected, onSelect }: MemberCardProps) {
  const birthYear = new Date(member.birthDate).getFullYear()
  const deathYear = member.deathDate ? new Date(member.deathDate).getFullYear() : null

  return (
    <button
      onClick={() => onSelect(member.id)}
      className={cn(
        "w-full text-left p-4 rounded-xl border transition-all duration-200",
        isSelected
          ? "bg-terracotta/15 border-terracotta"
          : "bg-card border-border hover:border-dusty-rose"
      )}
    >
      <div className="flex items-center gap-3">
        <div
          className={cn(
            "w-10 h-10 rounded-full flex items-center justify-center shrink-0 overflow-hidden",
            isSelected ? "ring-2 ring-terracotta" : "",
            !member.imageUrl && (isSelected ? "bg-terracotta text-parchment" : "bg-secondary text-muted-foreground")
          )}
        >
          {member.imageUrl ? (
            <Image
              src={member.imageUrl}
              alt={`${member.firstName} ${member.lastName}`}
              width={40}
              height={40}
              className="w-full h-full object-cover"
            />
          ) : (
            <User className="w-4 h-4" />
          )}
        </div>
        <div className="flex-1 min-w-0">
          <p className="font-serif text-sm font-medium text-foreground truncate flex items-center gap-1.5">
            {member.firstName} {member.lastName}
            <GenderIndicator gender={member.gender} />
          </p>
          <p className="text-xs text-muted-foreground">
            {birthYear}{deathYear ? ` - ${deathYear}` : ''}
          </p>
        </div>
      </div>
    </button>
  )
}

interface DetailFieldProps {
  icon: React.ReactNode
  label: string
  value: string
}

function DetailField({ icon, label, value }: DetailFieldProps) {
  return (
    <div className="flex items-start gap-3 py-3 border-b border-border last:border-0">
      <div className="text-sage shrink-0 mt-0.5">{icon}</div>
      <div className="flex-1">
        <p className="text-xs text-muted-foreground mb-0.5">{label}</p>
        <p className="text-sm text-foreground">{value}</p>
      </div>
    </div>
  )
}

// Partner type styling and labels
function getPartnerTypeConfig(type: PartnerType) {
  switch (type) {
    case 'currentMarriedPartner':
      return { label: 'Married', sortOrder: 0 }
    case 'currentPartner':
      return { label: 'Current', sortOrder: 1 }
    case 'exPartner':
      return { label: 'Former', sortOrder: 2 }
  }
}

interface PartnerButtonProps {
  partner: FamilyMember
  relation: PartnerRelation
  isSelected: boolean
  onSelect: () => void
}

function PartnerButton({ partner, relation, isSelected, onSelect }: PartnerButtonProps) {
  const config = getPartnerTypeConfig(relation.type)
  
  return (
    <button
      onClick={onSelect}
      className={cn(
        "flex items-center gap-2.5 px-3 py-2 rounded-lg border transition-all duration-200 bg-secondary border-border",
        isSelected 
          ? "border-terracotta/60 bg-terracotta/5" 
          : "hover:border-muted-foreground/30"
      )}
    >
      <div className="w-7 h-7 rounded-full overflow-hidden bg-background shrink-0">
        {partner.imageUrl ? (
          <Image
            src={partner.imageUrl}
            alt={partner.firstName}
            width={28}
            height={28}
            className="w-full h-full object-cover"
          />
        ) : (
          <div className="w-full h-full flex items-center justify-center">
            <User className="w-3.5 h-3.5 text-muted-foreground" />
          </div>
        )}
      </div>
      <div className="flex flex-col items-start">
        <span className="text-sm font-medium text-foreground leading-tight">
          {partner.firstName} {partner.lastName}
        </span>
        <span className="text-xs text-muted-foreground leading-tight">{config.label}</span>
      </div>
    </button>
  )
}

interface AddChildrenDialogProps {
  member: FamilyMember
  partner: FamilyMember
  existingChildIds: string[]
  onClose: () => void
  onAddChildren: (childIds: string[]) => void
}

function AddChildrenDialog({ member, partner, existingChildIds, onClose, onAddChildren }: AddChildrenDialogProps) {
  const [selectedIds, setSelectedIds] = useState<string[]>([])
  const [search, setSearch] = useState('')
  
  // Get available members to add as children (not already children, not the member or partner themselves)
  const availableMembers = useMemo(() => {
    return familyMembers.filter(m => 
      m.id !== member.id && 
      m.id !== partner.id && 
      !existingChildIds.includes(m.id) &&
      `${m.firstName} ${m.lastName}`.toLowerCase().includes(search.toLowerCase())
    )
  }, [member.id, partner.id, existingChildIds, search])
  
  const toggleSelection = (id: string) => {
    setSelectedIds(prev => 
      prev.includes(id) ? prev.filter(x => x !== id) : [...prev, id]
    )
  }
  
  return (
    <motion.div
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      exit={{ opacity: 0 }}
      className="fixed inset-0 bg-ink/50 flex items-center justify-center z-50 p-4"
      onClick={onClose}
    >
      <motion.div
        initial={{ scale: 0.95, opacity: 0 }}
        animate={{ scale: 1, opacity: 1 }}
        exit={{ scale: 0.95, opacity: 0 }}
        className="bg-card rounded-xl border border-border p-6 w-full max-w-md max-h-[80vh] flex flex-col"
        onClick={e => e.stopPropagation()}
      >
        <div className="flex items-center justify-between mb-4">
          <h3 className="font-serif text-lg text-foreground">Add Children</h3>
          <button onClick={onClose} className="text-muted-foreground hover:text-foreground">
            <X className="w-5 h-5" />
          </button>
        </div>
        
        <p className="text-sm text-muted-foreground mb-4">
          Adding children to {member.firstName} and {partner.firstName}
        </p>
        
        <Input
          type="search"
          placeholder="Search members..."
          value={search}
          onChange={e => setSearch(e.target.value)}
          className="mb-4"
        />
        
        <div className="flex-1 overflow-auto space-y-2 mb-4">
          {availableMembers.length > 0 ? (
            availableMembers.map(m => {
              const isSelected = selectedIds.includes(m.id)
              return (
                <button
                  key={m.id}
                  onClick={() => toggleSelection(m.id)}
                  className={cn(
                    "w-full flex items-center gap-3 p-3 rounded-lg border transition-all",
                    isSelected 
                      ? "border-terracotta bg-terracotta/10" 
                      : "border-border hover:border-dusty-rose"
                  )}
                >
                  <div className="w-8 h-8 rounded-full overflow-hidden bg-secondary shrink-0">
                    {m.imageUrl ? (
                      <Image src={m.imageUrl} alt={m.firstName} width={32} height={32} className="w-full h-full object-cover" />
                    ) : (
                      <div className="w-full h-full flex items-center justify-center">
                        <User className="w-4 h-4 text-muted-foreground" />
                      </div>
                    )}
                  </div>
                  <div className="flex-1 text-left">
                    <p className="text-sm font-medium text-foreground">{m.firstName} {m.lastName}</p>
                    <p className="text-xs text-muted-foreground">{new Date(m.birthDate).getFullYear()}</p>
                  </div>
                  {isSelected && <Check className="w-4 h-4 text-terracotta" />}
                </button>
              )
            })
          ) : (
            <p className="text-sm text-muted-foreground text-center py-4">No available members found</p>
          )}
        </div>
        
        <div className="flex gap-2">
          <Button variant="outline" className="flex-1" onClick={onClose}>
            Cancel
          </Button>
          <Button 
            className="flex-1 bg-terracotta text-parchment hover:bg-terracotta/90"
            disabled={selectedIds.length === 0}
            onClick={() => {
              onAddChildren(selectedIds)
              onClose()
            }}
          >
            Add {selectedIds.length > 0 ? `(${selectedIds.length})` : ''}
          </Button>
        </div>
      </motion.div>
    </motion.div>
  )
}

export function ArchiveView() {
  const [selectedId, setSelectedId] = useState<string | null>('13') // Start with Michael to show multiple partners
  const [searchQuery, setSearchQuery] = useState('')
  const [showRelationshipPanel, setShowRelationshipPanel] = useState(false)
  const [selectedPartnerId, setSelectedPartnerId] = useState<string | null>(null)
  const [showAddChildrenDialog, setShowAddChildrenDialog] = useState(false)
  const [showCreatePersonDialog, setShowCreatePersonDialog] = useState(false)

  const selectedMember = selectedId ? getMemberById(selectedId) : null

  const filteredMembers = familyMembers.filter(member =>
    `${member.firstName} ${member.lastName}`.toLowerCase().includes(searchQuery.toLowerCase())
  )

  // Get relationships
  const parents = selectedMember?.parentIds?.map(id => getMemberById(id)).filter(Boolean) as FamilyMember[] || []
  
  // Get partners with their relation info, sorted by type (current first, then ex)
  const partnersWithRelations = useMemo(() => {
    let partners: { partner: FamilyMember, relation: PartnerRelation }[] = []
    
    if (!selectedMember?.partners) {
      // Fallback to legacy spouseIds if no partners array
      partners = selectedMember?.spouseIds?.map(id => {
        const partner = getMemberById(id)
        if (!partner) return null
        return {
          partner,
          relation: { partnerId: id, type: 'currentPartner' as PartnerType, childIds: selectedMember.childIds || [] }
        }
      }).filter(Boolean) as { partner: FamilyMember, relation: PartnerRelation }[] || []
    } else {
      partners = selectedMember.partners.map(rel => {
        const partner = getMemberById(rel.partnerId)
        if (!partner) return null
        return { partner, relation: rel }
      }).filter(Boolean) as { partner: FamilyMember, relation: PartnerRelation }[]
    }
    
    // Sort by type: currentMarriedPartner (0), currentPartner (1), exPartner (2)
    return partners.sort((a, b) => {
      const orderA = getPartnerTypeConfig(a.relation.type).sortOrder
      const orderB = getPartnerTypeConfig(b.relation.type).sortOrder
      return orderA - orderB
    })
  }, [selectedMember])
  
  // Auto-select first partner or current partner when member changes
  useMemo(() => {
    if (partnersWithRelations.length > 0 && selectedMember) {
      // Prefer current married partner, then current partner, then first
      const currentMarried = partnersWithRelations.find(p => p.relation.type === 'currentMarriedPartner')
      const current = partnersWithRelations.find(p => p.relation.type === 'currentPartner')
      const firstPartner = currentMarried || current || partnersWithRelations[0]
      setSelectedPartnerId(firstPartner?.partner.id || null)
    } else {
      setSelectedPartnerId(null)
    }
  }, [selectedMember?.id, partnersWithRelations.length])
  
  // Get children based on selected partner
  const selectedPartnerRelation = partnersWithRelations.find(p => p.partner.id === selectedPartnerId)
  const childrenFromPartner = useMemo(() => {
    if (!selectedPartnerRelation?.relation.childIds) {
      // Fallback to all children
      return selectedMember?.childIds?.map(id => getMemberById(id)).filter(Boolean) as FamilyMember[] || []
    }
    return selectedPartnerRelation.relation.childIds
      .map(id => getMemberById(id))
      .filter(Boolean) as FamilyMember[]
  }, [selectedPartnerRelation, selectedMember])
  
  const selectedPartner = selectedPartnerId ? getMemberById(selectedPartnerId) : null

  return (
    <div className="h-full flex">
      {/* Left: Member List */}
      <div className="w-72 border-r border-border bg-card/30 flex flex-col">
        <div className="p-4 border-b border-border">
          <h2 className="font-serif text-lg text-foreground mb-3">Archive</h2>
          <Input
            type="search"
            placeholder="Search members..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="bg-card"
          />
        </div>
        <div className="flex-1 overflow-auto p-3 space-y-2">
          {filteredMembers.map(member => (
            <MemberCard
              key={member.id}
              member={member}
              isSelected={selectedId === member.id}
              onSelect={setSelectedId}
            />
          ))}
        </div>
        <div className="p-4 border-t border-border">
          <Button 
            className="w-full gap-2 bg-terracotta text-parchment hover:bg-terracotta/90"
            onClick={() => setShowCreatePersonDialog(true)}
          >
            <Plus className="w-4 h-4" />
            Create New Person
          </Button>
        </div>
      </div>

      {/* Center: Detail Panel */}
      <div className="flex-1 overflow-auto">
        {selectedMember ? (
          <motion.div
            key={selectedMember.id}
            initial={{ opacity: 0, y: 10 }}
            animate={{ opacity: 1, y: 0 }}
            className="p-8 max-w-2xl mx-auto"
          >
            {/* Header */}
            <div className="flex items-start gap-6 mb-8">
              <div className="w-24 h-24 rounded-xl bg-secondary flex items-center justify-center shrink-0 overflow-hidden">
                {selectedMember.imageUrl ? (
                  <Image
                    src={selectedMember.imageUrl}
                    alt={`${selectedMember.firstName} ${selectedMember.lastName}`}
                    width={96}
                    height={96}
                    className="w-full h-full object-cover"
                  />
                ) : (
                  <User className="w-10 h-10 text-muted-foreground" />
                )}
              </div>
              <div>
                <h1 className="font-serif text-3xl text-foreground mb-1 flex items-center gap-2">
                  {selectedMember.firstName} {selectedMember.lastName}
                  <GenderIndicator gender={selectedMember.gender} size="md" />
                </h1>
                <p className="text-muted-foreground">
                  {new Date(selectedMember.birthDate).getFullYear()}
                  {selectedMember.deathDate && ` - ${new Date(selectedMember.deathDate).getFullYear()}`}
                </p>
              </div>
            </div>

            {/* Details */}
            <div className="bg-card rounded-xl border border-border p-6 mb-6">
              <h3 className="font-serif text-lg text-foreground mb-4">Details</h3>
              {selectedMember.initialLastName && (
                <DetailField
                  icon={<User className="w-4 h-4" />}
                  label="Birth Name"
                  value={`${selectedMember.firstName} ${selectedMember.initialLastName}`}
                />
              )}
              {selectedMember.birthDate && (
                <DetailField
                  icon={<Calendar className="w-4 h-4" />}
                  label="Born"
                  value={new Date(selectedMember.birthDate).toLocaleDateString('en-US', { year: 'numeric', month: 'long', day: 'numeric' })}
                />
              )}
              {selectedMember.deathDate && (
                <DetailField
                  icon={<Calendar className="w-4 h-4" />}
                  label="Died"
                  value={new Date(selectedMember.deathDate).toLocaleDateString('en-US', { year: 'numeric', month: 'long', day: 'numeric' })}
                />
              )}
              {(selectedMember.birthCity || selectedMember.birthCountry) && (
                <DetailField
                  icon={<MapPin className="w-4 h-4" />}
                  label="Birthplace"
                  value={[selectedMember.birthCity, selectedMember.birthCountry].filter(Boolean).join(', ')}
                />
              )}
              {selectedMember.occupation && (
                <DetailField
                  icon={<Briefcase className="w-4 h-4" />}
                  label="Occupation"
                  value={selectedMember.occupation}
                />
              )}
              {selectedMember.notes && (
                <DetailField
                  icon={<FileText className="w-4 h-4" />}
                  label="Notes"
                  value={selectedMember.notes}
                />
              )}
            </div>

            {/* Contact - only show if any contact info exists */}
            {(selectedMember.email || selectedMember.telephone || selectedMember.streetNumber || selectedMember.city) && (
              <div className="bg-card rounded-xl border border-border p-6 mb-6">
                <h3 className="font-serif text-lg text-foreground mb-4">Contact</h3>
                {selectedMember.email && (
                  <DetailField
                    icon={<Mail className="w-4 h-4" />}
                    label="Email"
                    value={selectedMember.email}
                  />
                )}
                {selectedMember.telephone && (
                  <DetailField
                    icon={<Phone className="w-4 h-4" />}
                    label="Phone"
                    value={selectedMember.telephone}
                  />
                )}
                {(selectedMember.streetNumber || selectedMember.city) && (
                  <DetailField
                    icon={<Home className="w-4 h-4" />}
                    label="Address"
                    value={[
                      selectedMember.streetNumber,
                      [selectedMember.plz, selectedMember.city].filter(Boolean).join(' ')
                    ].filter(Boolean).join(', ')}
                  />
                )}
              </div>
            )}

            {/* Relationships */}
            <div className="bg-card rounded-xl border border-border p-6">
              <h3 className="font-serif text-lg text-foreground mb-4">Relationships</h3>

              {/* Parents */}
              {parents.length > 0 && (
                <div className="mb-6">
                  <p className="text-xs text-muted-foreground uppercase tracking-wide mb-2">Parents</p>
                  <div className="flex flex-wrap gap-2">
                    {parents.map(parent => (
                      <button
                        key={parent.id}
                        onClick={() => setSelectedId(parent.id)}
                        className="px-3 py-1.5 bg-secondary rounded-lg text-sm text-foreground hover:bg-secondary/80 transition-colors"
                      >
                        {parent.firstName} {parent.lastName}
                      </button>
                    ))}
                  </div>
                </div>
              )}

              {/* Partners */}
              <div className="mb-6">
                <div className="flex items-center justify-between mb-2">
                  <p className="text-xs text-muted-foreground uppercase tracking-wide flex items-center gap-1">
                    <Heart className="w-3 h-3 text-dusty-rose" />
                    Partner(s)
                  </p>
                  <Button
                    variant="ghost"
                    size="sm"
                    className="text-xs text-terracotta hover:text-terracotta h-6 px-2"
                    onClick={() => setShowRelationshipPanel(true)}
                  >
                    <Plus className="w-3 h-3 mr-1" />
                    Add Partner
                  </Button>
                </div>
                
                {partnersWithRelations.length > 0 ? (
                  <div className="flex flex-wrap gap-2">
                    {partnersWithRelations.map(({ partner, relation }) => (
                      <PartnerButton
                        key={partner.id}
                        partner={partner}
                        relation={relation}
                        isSelected={selectedPartnerId === partner.id}
                        onSelect={() => setSelectedPartnerId(partner.id)}
                      />
                    ))}
                  </div>
                ) : (
                  <p className="text-sm text-muted-foreground italic">No partners recorded</p>
                )}
                
              </div>

              {/* Children (from selected partner) */}
              <div>
                <div className="flex items-center justify-between mb-2">
                  <p className="text-xs text-muted-foreground uppercase tracking-wide flex items-center gap-1">
                    <Users className="w-3 h-3 text-sage" />
                    Children
                    {selectedPartner && (
                      <span className="font-normal normal-case ml-1">
                        with {selectedPartner.firstName}
                      </span>
                    )}
                  </p>
                  {selectedPartner && (
                    <Button
                      variant="ghost"
                      size="sm"
                      className="text-xs text-terracotta hover:text-terracotta h-6 px-2"
                      onClick={() => setShowAddChildrenDialog(true)}
                    >
                      <Plus className="w-3 h-3 mr-1" />
                      Add Children
                    </Button>
                  )}
                </div>
                
                {childrenFromPartner.length > 0 ? (
                  <div className="flex flex-wrap gap-2">
                    {childrenFromPartner.map(child => (
                      <button
                        key={child.id}
                        onClick={() => setSelectedId(child.id)}
                        className="px-3 py-1.5 bg-sage/10 border border-sage/30 rounded-lg text-sm text-foreground hover:bg-sage/20 transition-colors"
                      >
                        {child.firstName} {child.lastName}
                      </button>
                    ))}
                  </div>
                ) : (
                  <p className="text-sm text-muted-foreground italic">
                    {selectedPartner ? `No children with ${selectedPartner.firstName}` : 'No children recorded'}
                  </p>
                )}
              </div>
            </div>
          </motion.div>
        ) : (
          <div className="h-full flex items-center justify-center">
            <p className="text-muted-foreground">Select a member to view details</p>
          </div>
        )}
      </div>

      {/* Right: Relationship Editor Panel */}
      <AnimatePresence>
        {showRelationshipPanel && (
          <motion.div
            initial={{ x: 100, opacity: 0 }}
            animate={{ x: 0, opacity: 1 }}
            exit={{ x: 100, opacity: 0 }}
            className="w-80 border-l border-border bg-card/50 p-6"
          >
            <div className="flex items-center justify-between mb-6">
              <h3 className="font-serif text-lg text-foreground">Add Relationship</h3>
              <button
                onClick={() => setShowRelationshipPanel(false)}
                className="text-muted-foreground hover:text-foreground"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <div className="space-y-4">
              <div className="p-4 bg-secondary/50 rounded-xl text-center">
                <p className="text-sm text-muted-foreground mb-1">Adding relationship for</p>
                <p className="font-serif text-foreground">
                  {selectedMember?.firstName} {selectedMember?.lastName}
                </p>
              </div>

              <div>
                <label className="text-sm text-muted-foreground block mb-2">Relationship Type</label>
                <div className="space-y-2">
                  {[
                    { type: 'Parent', icon: Users },
                    { type: 'Married Partner', icon: Heart },
                    { type: 'Partner', icon: Heart },
                  ].map(({ type, icon: Icon }) => (
                    <button
                      key={type}
                      className="w-full text-left px-4 py-3 rounded-xl border border-border hover:border-terracotta hover:bg-terracotta/5 transition-all duration-200 flex items-center gap-3"
                    >
                      <Icon className="w-4 h-4 text-muted-foreground" />
                      <span className="text-sm text-foreground">{type}</span>
                    </button>
                  ))}
                </div>
              </div>

              <div>
                <label className="text-sm text-muted-foreground block mb-2">Search existing members</label>
                <Input placeholder="Search..." className="bg-card" />
              </div>

              <Button 
                className="w-full bg-terracotta text-parchment hover:bg-terracotta/90"
                onClick={() => {
                  setShowRelationshipPanel(false)
                  setShowCreatePersonDialog(true)
                }}
              >
                Create New Person
              </Button>
            </div>
          </motion.div>
        )}
      </AnimatePresence>

      {/* Add Children Dialog */}
      <AnimatePresence>
        {showAddChildrenDialog && selectedMember && selectedPartner && (
          <AddChildrenDialog
            member={selectedMember}
            partner={selectedPartner}
            existingChildIds={selectedPartnerRelation?.relation.childIds || []}
            onClose={() => setShowAddChildrenDialog(false)}
            onAddChildren={(childIds) => {
              // In a real app, this would update the data
              console.log('[v0] Adding children:', childIds, 'to', selectedMember.id, 'and', selectedPartner.id)
            }}
          />
        )}
      </AnimatePresence>

      {/* Create New Person Dialog */}
      <CreatePersonDialog
        open={showCreatePersonDialog}
        onOpenChange={setShowCreatePersonDialog}
        onSubmit={(data: PersonFormData) => {
          // In a real app, this would save to the database
          console.log('[v0] Creating new person:', data)
          // For now, just log it - later this will call the API
        }}
      />
    </div>
  )
}
